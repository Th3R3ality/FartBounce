let timer = 0

export const gradientScale = -0.05
export const gradientSpeed = 0.002

export function addToTimer(time: number) {
    timer += time;
}

export function getTimerTime(): number   {
    return timer;
}

var tolerance = 1;
var invertRange = [0, 1];
var invertStep = 0.1;
var sepiaRange = [0, 1];
var sepiaStep = 0.1;
var saturateRange = [5, 100];
var saturateStep = 5;
var hueRotateRange = [0, 360];
var hueRotateStep = 5;
var possibleColors: any;

function sepiaMatrix(s: number) {
    return [
        (0.393 + 0.607 * (1 - s)), (0.769 - 0.769 * (1 - s)), (0.189 - 0.189 * (1 - s)),
        (0.349 - 0.349 * (1 - s)), (0.686 + 0.314 * (1 - s)), (0.168 - 0.168 * (1 - s)),
        (0.272 - 0.272 * (1 - s)), (0.534 - 0.534 * (1 - s)), (0.131 + 0.869 * (1 - s)),
    ]
}

function saturateMatrix(s: number) {
    return [
        0.213+0.787*s, 0.715-0.715*s, 0.072-0.072*s,
        0.213-0.213*s, 0.715+0.285*s, 0.072-0.072*s,
        0.213-0.213*s, 0.715-0.715*s, 0.072+0.928*s,
    ]
}

function hueRotateMatrix(d: number) {
    var cos = Math.cos(d * Math.PI / 180);
    var sin = Math.sin(d * Math.PI / 180);
    var a00 = 0.213 + cos*0.787 - sin*0.213;
    var a01 = 0.715 - cos*0.715 - sin*0.715;
    var a02 = 0.072 - cos*0.072 + sin*0.928;

    var a10 = 0.213 - cos*0.213 + sin*0.143;
    var a11 = 0.715 + cos*0.285 + sin*0.140;
    var a12 = 0.072 - cos*0.072 - sin*0.283;

    var a20 = 0.213 - cos*0.213 - sin*0.787;
    var a21 = 0.715 - cos*0.715 + sin*0.715;
    var a22 = 0.072 + cos*0.928 + sin*0.072;

    return [
        a00, a01, a02,
        a10, a11, a12,
        a20, a21, a22,
    ]
}

function clamp(value: number) {
    return value > 255 ? 255 : value < 0 ? 0 : value;
}

function filter(m: number[], c: number[]) {
    return [
        clamp(m[0]*c[0] + m[1]*c[1] + m[2]*c[2]),
        clamp(m[3]*c[0] + m[4]*c[1] + m[5]*c[2]),
        clamp(m[6]*c[0] + m[7]*c[1] + m[8]*c[2]),
    ]
}

function invertBlack(i: number) {
    return [
        i * 255,
        i * 255,
        i * 255,
    ]
}

function generateColors() {
    let possibleColors = [];

    let invert = invertRange[0];
    for (invert; invert <= invertRange[1]; invert+=invertStep) {
        let sepia = sepiaRange[0];
        for (sepia; sepia <= sepiaRange[1]; sepia+=sepiaStep) {
            let saturate = saturateRange[0];
            for (saturate; saturate <= saturateRange[1]; saturate+=saturateStep) {
                let hueRotate = hueRotateRange[0];
                for (hueRotate; hueRotate <= hueRotateRange[1]; hueRotate+=hueRotateStep) {
                    let invertColor = invertBlack(invert);
                    let sepiaColor = filter(sepiaMatrix(sepia), invertColor);
                    let saturateColor = filter(saturateMatrix(saturate), sepiaColor);
                    let hueRotateColor = filter(hueRotateMatrix(hueRotate), saturateColor);

                    let colorObject = {
                        filters: { invert, sepia, saturate, hueRotate },
                        color: hueRotateColor
                    }

                    possibleColors.push(colorObject);
                }
            }
        }
    }

    return possibleColors;
}

function getFilters(targetColor: number[], localTolerance: number) {
    possibleColors = possibleColors || generateColors();

    for (var i = 0; i < possibleColors.length; i++) {
        var color = possibleColors[i].color;
        if (
            Math.abs(color[0] - targetColor[0]) < localTolerance &&
            Math.abs(color[1] - targetColor[1]) < localTolerance &&
            Math.abs(color[2] - targetColor[2]) < localTolerance
        ) {
            return possibleColors[i].filters;
        }
    }

    localTolerance += tolerance;
    return getFilters(targetColor, localTolerance)
}

export function getFilterFromColor(color: string) {
    var targetColor: any = color.split(',');
    targetColor = [
        parseInt(targetColor[0]), // [R]
        parseInt(targetColor[1]), // [G]
        parseInt(targetColor[2]), // [B]
    ]
    var filters = getFilters(targetColor, tolerance);
    var filtersCSS = 'filter: ' +
        'brightness(0%) ' +
        'invert('+Math.floor(filters.invert*100)+'%) '+
        'sepia('+Math.floor(filters.sepia*100)+'%) ' +
        'saturate('+Math.floor(filters.saturate*100)+'%) ' +
        'hue-rotate('+Math.floor(filters.hueRotate)+'deg);';

    return filtersCSS;
}

export interface RGBColor {
    r: number;
    g: number;
    b: number;
}

export function HSVtoRGB(h: number, s: number, v: number): RGBColor {
    var r = 0, g = 0, b = 0, i, f, p, q, t;

    i = Math.floor(h * 6);
    f = h * 6 - i;
    p = v * (1 - s);
    q = v * (1 - f * s);
    t = v * (1 - (1 - f) * s);
    switch (i % 6) {
        case 0: r = v, g = t, b = p; break;
        case 1: r = q, g = v, b = p; break;
        case 2: r = p, g = v, b = t; break;
        case 3: r = p, g = q, b = v; break;
        case 4: r = t, g = p, b = v; break;
        case 5: r = v, g = p, b = q; break;
    }
    return {
        r: Math.round(r * 255),
        g: Math.round(g * 255),
        b: Math.round(b * 255)
    };
}


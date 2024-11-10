<script lang="ts">
    import {onMount} from "svelte";
    import {getGameWindow, getModules, getModuleSettings} from "../../integration/rest";
    import {groupByCategory} from "../../integration/util";
    import type {GroupedModules, Module} from "../../integration/types";
    import Panel from "./Panel.svelte";
    import Search from "./Search.svelte";
    import Description from "./Description.svelte";
    import {fade} from "svelte/transition";
    import {listen} from "../../integration/ws";
    import type {ClickGuiScaleChangeEvent, ScaleFactorChangeEvent} from "../../integration/events";
    import {scaleFactor} from "./clickgui_store";
    import {addToTimer, getTimerTime, gradientScale, gradientSpeed, HSVtoRGB} from "../../reality";

    let categories: GroupedModules = {};
    let modules: Module[] = [];
    let minecraftScaleFactor = 2;
    let clickGuiScaleFactor = 1;
    $: {
        scaleFactor.set(minecraftScaleFactor * clickGuiScaleFactor);
    }

    onMount(async () => {
        const gameWindow = await getGameWindow();
        minecraftScaleFactor = gameWindow.scaleFactor;

        modules = await getModules();
        categories = groupByCategory(modules);

        const clickGuiSettings = await getModuleSettings("ClickGUI");
        clickGuiScaleFactor = clickGuiSettings.value.find(v => v.name === "Scale")?.value as number ?? 1
        setInterval(updateModuleColors, 10);
    });

    listen("scaleFactorChange", (e: ScaleFactorChangeEvent) => {
        minecraftScaleFactor = e.scaleFactor;
    });

    listen("clickGuiScaleChange", (e: ClickGuiScaleChangeEvent) => {
        clickGuiScaleFactor = e.value;
    });

    function updateModuleColors() {
        const panels = document.getElementsByClassName("panel");
        if (panels == null) return;

        for (let i = 0; i < panels.length; i++) {
            const panel = panels[i];

            const modules = panel.lastElementChild?.children as HTMLCollectionOf<HTMLElement>;

            for (let i = 0; i < modules.length; i++) {
                const module = modules[i].firstChild as HTMLElement;
                if (module.className.includes("enabled")) {
                    module.style.color = "#1a191a"
                    const rgb = HSVtoRGB(getTimerTime() + i * gradientScale, 0.7, 1);
                    module.style.background = `rgb( ${rgb.r}, ${rgb.g}, ${rgb.b})`;
                    const darkrgb = HSVtoRGB(getTimerTime() + i * gradientScale, 0.7, 0.5);
                    module.style.borderBottomColor = `rgb( ${darkrgb.r}, ${darkrgb.g}, ${darkrgb.b})`;
                }
                else
                {
                    module.style = ""
                }
            }
        }
        addToTimer(gradientSpeed);
    }
</script>

<div class="clickgui" transition:fade|global={{duration: 200}}
     style="transform: scale({$scaleFactor * 50}%); width: {2 / $scaleFactor * 100}vw; height: {2 / $scaleFactor * 100}vh;">
    <Description/>
    <Search modules={structuredClone(modules)}/>

    {#each Object.entries(categories) as [category, modules], panelIndex}
        <Panel {category} {modules} {panelIndex}/>
    {/each}
</div>

<style lang="scss">
  @import "../../colors.scss";

  .clickgui {
    background-color: rgba($clickgui-base-color, 0.6);
    overflow: hidden;
    position: absolute;
    will-change: opacity;
    transform-origin: top left;
    left: 0;
    top: 0;
  }
</style>

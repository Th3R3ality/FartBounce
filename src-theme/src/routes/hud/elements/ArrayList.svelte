<script lang="ts">
    import {onMount, tick} from "svelte";
    import type {Module} from "../../../integration/types";
    import {getModules} from "../../../integration/rest";
    import {listen} from "../../../integration/ws";
    import {getTextWidth} from "../../../integration/text_measurement";
    import {flip} from "svelte/animate";
    import {fly} from "svelte/transition";
    import {convertToSpacedString, spaceSeperatedNames} from "../../../theme/theme_config";
    import {addToTimer, getFilterFromColor, getTimerTime, gradientScale, HSVtoRGB} from "../../../reality";
    import {gradientSpeed} from "../../../reality.js";

    let enabledModules: Module[] = [];

    async function updateEnabledModules() {
        const modules = await getModules();
        const visibleModules = modules.filter(m => m.enabled && !m.hidden);

        const modulesWithWidths = visibleModules.map(module => {
                let formattedName = $spaceSeperatedNames ? convertToSpacedString(module.name) : module.name;
                let fullName = module.tag == null ? formattedName : formattedName + " " + module.tag;

                return {
                    ...module,
                    width: getTextWidth(fullName, "500 14px Proxima Nova")
                };
            }
        );

        modulesWithWidths.sort((a, b) => b.width - a.width);

        enabledModules = modulesWithWidths;
        await tick();
    }

    spaceSeperatedNames.subscribe(async () => {
        await updateEnabledModules();
    });

    onMount(async () => {
        await updateEnabledModules();
        setInterval(arraylistGradient, 10);
    });

    listen("toggleModule", async () => {
        await updateEnabledModules();
    });

    listen("refreshArrayList", async () => {
        await updateEnabledModules();
    });


    function arraylistGradient(){
        const arraylist = document.getElementById("arraylist");
        if (arraylist == null) return;
        const modules = arraylist.children as HTMLCollectionOf<HTMLElement>;

        const watermark = modules[0].firstElementChild as HTMLElement;
        if (watermark != null)
        {
            const watermarkColor = HSVtoRGB(getTimerTime(), 0.7, 1);
            const filter = getFilterFromColor(`${watermarkColor.r}, ${watermarkColor.g}, ${watermarkColor.b}`);
            // noinspection JSConstantReassignment,TypeScriptValidateTypes
            watermark.style = filter; // false error
        }


        for (let i = 1; i < modules.length; i++) {
            const element = modules[i];
            if (element.id != "module") continue;
            const rgb = HSVtoRGB(getTimerTime() + i*gradientScale, 0.7, 1);
            element.style.color = `rgb( ${rgb.r}, ${rgb.g}, ${rgb.b})`;
            //element.style.borderRightWidth = "2px";
            element.style.borderRight = `solid 2px rgb( ${rgb.r}, ${rgb.g}, ${rgb.b})`;
        }

        addToTimer(gradientSpeed);
    }


</script>

<div class="arraylist" id="arraylist">
    <div class="watermark-div">
        <img class="watermark" src="img/Rape.png" alt="watermark"/>
        <img class="watermark-v4" src="img/v4.png" alt="v4"/>
    </div>
    {#each enabledModules as {name, tag} (name)}
        <div class="module" id="module" animate:flip={{ duration: 200 }} in:fly={{ x: 50, duration: 200 }}>
            {$spaceSeperatedNames ? convertToSpacedString(name) : name}
            {#if tag}
                <span class="tag"> {tag}</span>
            {/if}
        </div>
    {/each}
</div>

<style lang="scss">
  @import "../../../colors.scss";

  .watermark-div{
    text-align: right;
    height: 30px;
    padding-right: 7px;
  }

  .watermark {
    //position: fixed;
    //top: 15px;
    //left: 15px;
    height: 24px;
    margin-right: -5px;
  }
  .watermark-v4 {
    //position: fixed;
    //top: 15px;
    //left: 15px;
    height: 24px;
    width: 34px;
  }

  .arraylist {
    font-family: "Proxima Nova", sans-serif;
    font-weight: 400;
    //position: fixed;
    //top: 0;
    //right: 0;
  }

  .module {
    background-color: rgba($arraylist-base-color, 0.5);
    color: $arraylist-text-color;
    //text-shadow: $accent-color-1 0px 0px 4px, $accent-color-1 0px 0px 12px;
    font-size: 16px;
    //border-radius: 0 0 0 0;
    padding: 0 6px 0 5px;
    border-right: solid 2px $accent-color;
    width: max-content;
    font-weight: 400;
    margin-left: auto;
    margin-right: 2px;
  }

  .tag {
    color: $arraylist-tag-color;
}
</style>

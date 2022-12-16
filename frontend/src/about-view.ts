import {css, html, LitElement, PropertyValues} from 'lit';
import {customElement} from 'lit/decorators.js';
import {registerTranslateConfig, translate, translateUnsafeHTML, use} from "lit-translate";
import {property, state} from "lit/decorators";

registerTranslateConfig({
    loader: lang => fetch(`${lang}.json`).then(res => res.json())
});

@customElement('about-view')
export class AboutView extends LitElement {

    static styles = css`
        :host {
            max-width: 1024px;
        }
        
        .logo-container {
            margin: auto;
        }
        
        .logo {
            max-width: 256px;
            max-height: 256px;
        }
  `;

    @property()
    language: string = 'en';

    // Defer the first update of the component until the strings has been loaded to avoid empty strings being shown
    @state() hasLoadedStrings = false;

    protected shouldUpdate(props: PropertyValues) {
        return this.hasLoadedStrings && super.shouldUpdate(props);
    }

    // Load the initial language and mark that the strings has been loaded so the component can render.
    async connectedCallback() {
        super.connectedCallback();
        this.classList.add('flex', 'flex-col', 'm-auto', 'px-m');

        await use(this.language);
        this.hasLoadedStrings = true;
    }

    setLanguage(language: string) {
        use(language);
    }

    render() {
        return html`
            <h2>${translate("about.liukuri.title")}</h2>
            <div>${translateUnsafeHTML("about.liukuri.descriptionHTML")}</div>

            <h2>${translate("about.logo.title")}</h2>
            <div>${translateUnsafeHTML("about.logo.descriptionHTML")}</div>
            <div class="logo-container">
                <img class="logo" src="icons/icon.png"/>
            </div>
        `;
    }

}

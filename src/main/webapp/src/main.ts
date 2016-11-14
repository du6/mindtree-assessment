import './polyfills.ts';
import {enableProdMode} from '@angular/core';
import {environment} from './environments/environment';

declare var gapi: any;

if (environment.production) {
  enableProdMode();
}

/**
 * JIT compile.
 */

// import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
// import {MaterialAppModule} from './app/app.module';
// platformBrowserDynamic().bootstrapModule(MaterialAppModule);


/**
 * AoT compile.
 * First run `./node_modules/.bin/ngc -p ./src/`
 */

//import {platformBrowser} from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
//import {SitesRunAppModuleNgFactory} from './aot/app/app.module.ngfactory';
import {SitesRunAppModule} from './app/app.module';

gapi.load('auth2', () => {
  gapi.auth2.init({
    client_id: '949964552538-v4so8dnqrjrt7d8t8fpr497t60qqa8sh.apps.googleusercontent.com',
    cookiepolicy: 'single_host_origin',
  }).then(() => {
    gapi.client.load('sitesRunApi', 'v1', 
    () => platformBrowserDynamic().bootstrapModule(SitesRunAppModule), 
    'https://sites-run.appspot.com/_ah/api');
    //platformBrowser().bootstrapModuleFactory(SitesRunAppModuleNgFactory);
  });
});

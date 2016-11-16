import './polyfills.ts';
import { enableProdMode } from '@angular/core';
import { environment } from './environments/environment';

declare var gapi: any;

if (environment.production) {
  enableProdMode();
}

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { MindTreeAppModule } from './app/app.module';

gapi.load('auth2', () => {
  gapi.auth2.init({
    client_id: '997254607295-o9870hqdcejj12rqjd2865j3s2brqh2s.apps.googleusercontent.com',
    cookiepolicy: 'single_host_origin',
  }).then(() => {
    gapi.client.load('mindTreeApi', 'v1', 
    () => platformBrowserDynamic().bootstrapModule(MindTreeAppModule), 
    'https://mind-tree-assessment.appspot.com/_ah/api');
  });
});

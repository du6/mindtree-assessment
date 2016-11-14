import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {MaterialModule} from '@angular/material';
import {Routes, RouterModule}   from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import {SitesRunAppComponent} from './app.component';
import {MySitesComponent} from './my-sites/my-sites.component';
import {HomeComponent} from './home/home.component';
import {SiteListComponent} from './site-list/site-list.component';
import {SiteDetailComponent} from './site-detail/site-detail.component';
import {CreateSiteComponent} from './create-site/create-site.component';
import {AuthService} from './services/auth.service';
import {GapiService} from './services/gapi.service';
import {ToastService} from './services/toast.service';

const routes: Routes = [
 { path: '_my', component: MySitesComponent },
 { path: '', component: HomeComponent },
];

@NgModule({
  imports: [
    BrowserModule,
    MaterialModule.forRoot(),
    RouterModule.forRoot(routes, { useHash: true }),
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [AuthService, GapiService, ToastService],
  declarations: [SitesRunAppComponent, MySitesComponent, HomeComponent, SiteListComponent, SiteDetailComponent, CreateSiteComponent],
  bootstrap: [SitesRunAppComponent],
})
export class SitesRunAppModule { }

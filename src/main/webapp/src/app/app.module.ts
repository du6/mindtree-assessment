import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MaterialModule } from '@angular/material';
import { Routes, RouterModule }   from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { MindTreeAppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { KnowledgeGraphComponent } from './knowledgegraph/knowledge-graph.component';
import { AuthService } from './services/auth.service';
import { GapiService} from './services/gapi.service';
import { ToastService } from './services/toast.service';

const routes: Routes = [
 { path: '', component: HomeComponent },
];

@NgModule({
  imports: [
    BrowserModule,
    MaterialModule.forRoot(),
    RouterModule.forRoot(routes, { useHash: false }),
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [AuthService, GapiService, ToastService],
  declarations: [MindTreeAppComponent, HomeComponent, KnowledgeGraphComponent],
  bootstrap: [MindTreeAppComponent],
})
export class MindTreeAppModule { }

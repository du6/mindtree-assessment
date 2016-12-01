import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MaterialModule } from '@angular/material';
import { Routes, RouterModule }   from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { MindTreeAppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { SidenavComponent } from './sidenav/sidenav.component';
import { KnowledgeGraphComponent } from './knowledgegraph/knowledge-graph.component';
import { AddNodeDialog } from './knowledgegraph/add-node-dialog.component';
import { QuizComponent } from './quiz/quiz.component';
import { QuestionComponent } from './question/question.component';
import { QuestionListComponent } from './question/question-list.component';
import { QuestionItemComponent } from './question/question-item.component';
import { CreateQuestionDialog } from './question/create-question-dialog.component';
import { AuthService } from './services/auth.service';
import { GapiService} from './services/gapi.service';
import { ExcludeNodesPipe} from './pipes/excludenodes.pipe';

const routes: Routes = [
 { path: 'question', component: QuestionComponent },
 { path: '', component: KnowledgeGraphComponent },
];

@NgModule({
  imports: [
    BrowserModule,
    MaterialModule.forRoot(),
    RouterModule.forRoot(routes, { useHash: false }),
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [AuthService, GapiService],
  declarations: [
    MindTreeAppComponent, 
    HomeComponent, 
    SidenavComponent, 
    KnowledgeGraphComponent, 
    AddNodeDialog,
    QuizComponent,
    QuestionComponent,
    CreateQuestionDialog,
    QuestionListComponent,
    QuestionItemComponent,
    ExcludeNodesPipe,
  ],
  entryComponents: [AddNodeDialog, CreateQuestionDialog],
  bootstrap: [MindTreeAppComponent],
})
export class MindTreeAppModule { }

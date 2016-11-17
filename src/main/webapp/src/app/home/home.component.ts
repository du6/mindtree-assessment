import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

@Component({
  selector: 'mind-tree-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.scss'],
})
export class HomeComponent {
  knowledgeGraph: any;

  constructor(private auth_: AuthService) {
  }

  isSignedIn(): boolean {
    return this.auth_.isSignedIn();
  }
}

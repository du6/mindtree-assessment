import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { KnowledgeNode } from '../common/knowledge-node';

@Component({
  selector: 'mind-tree-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.scss'],
})
export class HomeComponent {
  constructor(private auth_: AuthService) {
  }

  isSignedIn(): boolean {
    return this.auth_.isSignedIn();
  }
}

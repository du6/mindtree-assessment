import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'mind-tree-sidenav',
  templateUrl: 'sidenav.component.html',
  styleUrls: ['sidenav.component.scss'],
})
export class SidenavComponent {
  constructor(private _router: Router) {
  }

  gotoKnowledgeGraph() {
    this._router.navigate(['/']);
  }

  gotoQuiz() {
    this._router.navigate(['/quiz']);
  }
}
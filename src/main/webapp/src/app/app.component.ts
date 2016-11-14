import {Location} from '@angular/common';
import {Component} from '@angular/core';
import {Router} from '@angular/router';

import {AuthService} from './services/auth.service';

@Component({
  selector: 'sites-run-app',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class SitesRunAppComponent {
  constructor(private router_: Router, private location_: Location, private auth_: AuthService) {
    if (this.isSignedIn()) {
      this.location_.go('/_my');
    } else {
      this.location_.go('/');
    }
  }

  isSignedIn(): boolean {
    return this.auth_.isSignedIn();
  }

  signIn() {
    this.auth_.signIn().then(() => this.router_.navigate(['/_my']));
  }

  signOut() {
    this.auth_.signOut().then(() => this.router_.navigate(['/']));
  }
}

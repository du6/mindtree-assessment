import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {Location} from '@angular/common';

import {AuthService} from '../services/auth.service';
import {Mode} from '../common/mode'
import {Site} from '../common/site';

@Component({
  selector: 'sites-run-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.scss'],
})
export class HomeComponent {
  mode: Mode = Mode.ANONYMOUS;

  constructor(private location_: Location, private router_: Router, private auth_: AuthService) {
    if (this.auth_.isSignedIn()) {
      this.location_.go('/_my');
    }
  }

  signIn() {
    this.auth_.signIn().then(() => this.router_.navigate(['/_my']));
  }

  onSiteSaved(site: Site) {}
}

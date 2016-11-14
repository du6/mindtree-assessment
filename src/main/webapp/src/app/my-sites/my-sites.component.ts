import {Component, ChangeDetectorRef} from '@angular/core';
import { List } from 'immutable';
import {Location} from '@angular/common';

import {AuthService} from '../services/auth.service';
import {Mode} from '../common/mode';
import {Site} from '../common/site';
import {GapiService} from '../services/gapi.service';

@Component({
  selector: 'sites-run-my',
  templateUrl: 'my-sites.component.html',
  styleUrls: ['my-sites.component.scss'],
})
export class MySitesComponent {
  mode: Mode = Mode.USER;
  loading: boolean;
  sites: List<Site> = List<Site>();

  constructor(
      private location_: Location, 
      private auth_: AuthService,
      private gapi_: GapiService, 
      private changeDetectorRef_: ChangeDetectorRef) {
    if (!this.auth_.isSignedIn()) {
      this.location_.go('/');
    }
  }

  ngOnInit() {
    this.loadSites();
  }

  loadSites() {
    this.loading = true;
    this.gapi_.loadSitesCreatedByUser()
        .then((items) => this.sites = List<Site>(items), () => this.loading = false)
        .then(() => this.loading = false)
        .then(() => this.changeDetectorRef_.detectChanges());
  }

  onSiteSaved(site: Site) {
    this.addSite_(site);
  }

  onSiteDeleted(site: Site) {
    this.deleteSite_(site);
  }

  private addSite_(site: Site) {
    this.sites = this.sites.unshift(site);
    //TODO(du6): update view automatically
    this.changeDetectorRef_.detectChanges();
  }

  private deleteSite_(site: Site) {
    const index = this.sites.indexOf(site);
    if (index !== -1) {
      this.sites = this.sites.delete(index);
    }
    //TODO(du6): update view automatically
    this.changeDetectorRef_.detectChanges();
  }
}

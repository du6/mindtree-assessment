import { Injectable } from '@angular/core';

import {Site} from '../common/site';

// Google's login API namespace
declare var gapi: {client: {sitesRunApi: any}};

@Injectable()
export class GapiService {
  private gapi_: {client: {sitesRunApi: any}};

  constructor() {
    this.gapi_ = gapi;
  }

  loadSitesCreatedByUser(limit: number = 1000): Promise<Site[]> {
    return new Promise((resolve,reject) => 
        this.gapi_.client.sitesRunApi.getSitesCreated(limit)
            .execute((resp) => {
              if (resp.error) {
                reject(resp.error);
              } else if (resp.result) {
                resolve(<Site[]> resp.result.items);
              }
            }));
  }

  createSite(site: Site): Promise<Site> {
    return this.gapi_.client.sitesRunApi.createSite(site)
        .then((resp => resp.result));
  }

  createSiteAnonymously(site: Site): Promise<Site> {
    return this.gapi_.client.sitesRunApi.createSiteAnonymously(site)
        .then((resp => resp.result));
  }

  deleteSite(websafeKey: string): Promise<any> {
    return this.gapi_.client.sitesRunApi.deleteSite({websafeSiteKey: websafeKey});
  }

  deleteSiteByName(name: string): Promise<any> {
    return this.gapi_.client.sitesRunApi.deleteSiteByName({name: name});
  }

  getSiteByName(name: string): Promise<Site> {
    return this.gapi_.client.sitesRunApi.getSiteByName({name: name});
  }
}

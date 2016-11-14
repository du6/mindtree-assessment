import {
  Input,
  Output,
  Component,
  EventEmitter,
  ViewEncapsulation,
  ChangeDetectionStrategy,
  trigger,
  state,
  style,
  transition,
  animate,
} from '@angular/core';
import { List } from 'immutable';

import {Site} from '../common/site';
import {Mode} from '../common/mode';

@Component({
  selector: 'sites-run-site-list',
  templateUrl: 'site-list.component.html',
  styleUrls: ['site-list.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('fadeInOut', [
      state('in', style({
        height: '100%',
        opacity: 1,
      })),
      transition('void => *', [
        style({
          height: 0,
          opacity: 0
        }),
        animate(250, style({
          height: '100%',
          opacity: 1
        }))
      ]),
      transition('* => void', [
        style({
          height: '100%',
          opacity: 1
        }),
        animate(250, style({
          height: 0,
          opacity: 0
        }))
      ])
    ])
  ]
})
export class SiteListComponent {
  @Input() mode: Mode;
  @Input() sites: List<Site>;
  @Output() siteDeleted: EventEmitter<Site> = new EventEmitter();

  deleteSite(site: Site) {
    this.siteDeleted.emit(site);
  }
}

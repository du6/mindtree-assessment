import { Component, destroyPlatform, Optional } from '@angular/core';
import { MdDialogRef } from '@angular/material';

@Component({
  templateUrl: 'add-node-dialog.component.html',
  styleUrls: ['add-node-dialog.component.scss'],
})
export class AddNodeDialog {
  //TODO(du6): bind data with #name.value
  name: string;
  description: string;
  constructor(@Optional() private _dialogRef: MdDialogRef<AddNodeDialog>) { }

  cancel() {
    this._dialogRef.close();
  }

  confirm() {
    this._dialogRef.close({
      name: this.name,
      description: this.description,
    });
  }
}
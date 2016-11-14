import { Injectable, ViewContainerRef } from '@angular/core';
import {MdSnackBar, MdSnackBarConfig} from '@angular/material';

@Injectable()
export class ToastService {
  constructor(
    private snackBar_: MdSnackBar) {
  }

  actionSuccessToast(message: string, viewContainerRef: ViewContainerRef) {
    const config = new MdSnackBarConfig(viewContainerRef);
    this.snackBar_.open(message, 'Dismiss', config);
  }

  actionFailureToast(message: string, viewContainerRef: ViewContainerRef) {
    const config = new MdSnackBarConfig(viewContainerRef);
    this.snackBar_.open(message, 'Dismiss', config);
  }

  displayToast(message: string, ms: number = 3000) {
    let x = document.getElementById("snackbar")
    x.innerHTML = message;
    x.className = "show";
    setTimeout(() => { x.className = x.className.replace("show", ""); }, ms);
  }
}

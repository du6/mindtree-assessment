import { Injectable, ViewContainerRef } from '@angular/core';

@Injectable()
export class ToastService {
  constructor() {
  }

  displayToast(message: string, ms: number = 3000) {
    let x = document.getElementById("snackbar")
    x.innerHTML = message;
    x.className = "show";
    setTimeout(() => { x.className = x.className.replace("show", ""); }, ms);
  }
}

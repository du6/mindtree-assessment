import { Component, Input } from '@angular/core';

enum Role {
  TEACHER = 0,
  STUDENT = 1,
}

@Component({
  selector: 'mind-tree-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.scss'],
})
export class HomeComponent {
  public static ROLE: Role = Role.TEACHER;
  role: Role = HomeComponent.ROLE;

  constructor() {
  }

  onRoleChange(changes: any) {
    HomeComponent.ROLE = changes.value;
  }

  public static isTeacher(): boolean {
    return HomeComponent.ROLE == Role.TEACHER;
  }
}

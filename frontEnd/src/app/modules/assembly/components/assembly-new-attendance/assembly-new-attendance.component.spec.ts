import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssemblyNewAttendanceComponent } from './assembly-new-attendance.component';

describe('AssemblyNewAttendanceComponent', () => {
  let component: AssemblyNewAttendanceComponent;
  let fixture: ComponentFixture<AssemblyNewAttendanceComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AssemblyNewAttendanceComponent]
    });
    fixture = TestBed.createComponent(AssemblyNewAttendanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

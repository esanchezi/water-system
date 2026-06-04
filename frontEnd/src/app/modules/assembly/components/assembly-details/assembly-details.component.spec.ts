import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssemblyDetailsComponent } from './assembly-details.component';

describe('AssemblyDetailsComponent', () => {
  let component: AssemblyDetailsComponent;
  let fixture: ComponentFixture<AssemblyDetailsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AssemblyDetailsComponent]
    });
    fixture = TestBed.createComponent(AssemblyDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssemblyListComponent } from './assembly-list.component';

describe('AssemblyListComponent', () => {
  let component: AssemblyListComponent;
  let fixture: ComponentFixture<AssemblyListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AssemblyListComponent]
    });
    fixture = TestBed.createComponent(AssemblyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

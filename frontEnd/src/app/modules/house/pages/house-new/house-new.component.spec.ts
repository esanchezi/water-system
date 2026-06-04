import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HouseNewUserComponent } from './house-new.component';

describe('HouseNewUserComponent', () => {
  let component: HouseNewUserComponent;
  let fixture: ComponentFixture<HouseNewUserComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HouseNewUserComponent]
    });
    fixture = TestBed.createComponent(HouseNewUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

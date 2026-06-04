import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaterValvesComponent } from './water-valves.component';

describe('WaterValvesComponent', () => {
  let component: WaterValvesComponent;
  let fixture: ComponentFixture<WaterValvesComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WaterValvesComponent]
    });
    fixture = TestBed.createComponent(WaterValvesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

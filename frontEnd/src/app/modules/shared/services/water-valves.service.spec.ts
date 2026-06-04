import { TestBed } from '@angular/core/testing';

import { WaterValvesService } from './water-valves.service';

describe('WaterValvesService', () => {
  let service: WaterValvesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WaterValvesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

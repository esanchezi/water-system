import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewReceiptHistoryComponent } from './new-receipt-history.component';

describe('NewReceiptHistoryComponent', () => {
  let component: NewReceiptHistoryComponent;
  let fixture: ComponentFixture<NewReceiptHistoryComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NewReceiptHistoryComponent]
    });
    fixture = TestBed.createComponent(NewReceiptHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

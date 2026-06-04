import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReceiptCancelledComponent } from './receipt-cancelled.component';

describe('ReceiptCancelledComponent', () => {
  let component: ReceiptCancelledComponent;
  let fixture: ComponentFixture<ReceiptCancelledComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ReceiptCancelledComponent]
    });
    fixture = TestBed.createComponent(ReceiptCancelledComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReceiptHistoryComponent } from './receipt-history.component';

describe('ReceiptHistoryComponent', () => {
  let component: ReceiptHistoryComponent;
  let fixture: ComponentFixture<ReceiptHistoryComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ReceiptHistoryComponent]
    });
    fixture = TestBed.createComponent(ReceiptHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

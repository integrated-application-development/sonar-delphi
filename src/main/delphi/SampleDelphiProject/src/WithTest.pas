unit WithTest;

interface


implementation

type
    // Declare a customer record
  TCustomer = Record
    firstName : string[20];
    lastName  : string[20];
    address1  : string[100];
    address2  : string[100];
    address3  : string[100];
    city      : string[20];
    postCode  : string[8];
  end;

var
  John, Sarah : TCustomer;

begin
  // Set up the John's customer details
  with John do
  begin
    firstName := 'John';
    lastName  := 'Smith';
    address1  := '7 Park Drive';
    address2  := 'Branston';
    address3  := 'Grimworth';
    city      := 'Banmore';
    postCode  := 'BNM 1AB';
  end;

end.

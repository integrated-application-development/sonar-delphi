unit PointerNameTest;

interface

type
  TMyRecord = record
    name : String[20];
    age : Integer;
  end;

var
  myRecord : TMyRecord;
  aNumber: Integer;


  // Non-compliant: Pointer's name does not start with P
  NonCompliantPointer : ^TMyRecord;


  // Compliant
  PCompliantPointer : TMyRecord;


implementation

begin

  myRecord.name := 'Billy';
  myRecord.age := 99;


  NonCompliantPointer^ := myRecord;
  PCompliantPointer^ := myRecord;

end.

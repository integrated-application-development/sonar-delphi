unit RecordNameTest;

interface


type
  // Non-compliant: doesnt start with T<PascalCase>
  TMyRecord = record
    name : String[20];
    age : Integer;
  end;

  // Compliant
  TMyRecord = record
    name : String[20];
    age : Integer;
  end;


var
  myRecord : TMyRecord;
  myRecordTwo : TMyRecord;

implementation

begin

  myRecord.name := 'Billy';
  myRecord.age := 99;

  myRecordTwo.name := 'Lilly';
  myRecordTwo.age := 99;


end.

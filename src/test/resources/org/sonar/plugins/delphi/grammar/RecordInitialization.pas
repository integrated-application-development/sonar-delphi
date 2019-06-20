unit RecordInitialization;

interface

type
  MyCoolRecord = record
    ID: String;
    CoolnessFactor: Integer;
  end;

const
  C_EmptyRecord: MyCoolRecord = ();

  C_LameRecord: MyCoolRecord = (
    ID: '0';
    CoolnessFactor: 0 // No trailing semicolon
  );

  C_ArrayOfCoolRecords: array[0..4] of MyCoolRecord = (
    (
      ID: '1';
      CoolnessFactor: 42;
    ),
    (
      ID: '2';
      CoolnessFactor: 834;
    ),
    (
      ID: '3';
      CoolnessFactor: 1000;
    )
  );

implementation

end.

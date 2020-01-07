unit RecordExpressionItems;

{This is a sample Delphi file.}

interface

type
  TRecordProc = procedure(Foo: String);

  TAnonymousProcedure = record
    Proc: TRecordProc;
  end;

implementation

procedure SomeProc(Foo: String);
begin
  // Do nothing
end;

const
  RecordProcArray: array [0..1] of TRecordProc = (
    (Proc: SomeProc)
  );

end.
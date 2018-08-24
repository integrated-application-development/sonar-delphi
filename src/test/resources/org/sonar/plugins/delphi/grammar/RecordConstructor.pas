unit RecordConstructor;

interface

implementation

type
  TDummyRec = record
    FData : Integer;
    constructor Create(aData : Integer);
  end;
  
  TDummyClass = class
    FData : Integer;
    constructor Create(aData : Integer);
  end;  
  
  TEmptyRec = record
  end;

  TEmptyClass = class

  end;

constructor TDummyRec.Create(aData : Integer);
begin
  inherited;
  FData := aData;
end;

constructor TDummyClass.Create(aData : Integer);
begin
  inherited;
  FData := aData;
end;

end.
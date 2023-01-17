unit Records;

interface

type
  TRecord = record
    Baz: String;
  end;

  TFoo = class(TObject)
  private
    FAnonymousRecord: record
      Baz: String;
    end;
    
    FDeclaredRecord: TRecord;

    function TestAnonymousRecord: String;
    function TestDeclaredRecord: String;
  end;

implementation

  function TFoo.TestAnonymousRecord: String;
  begin
    Result := FAnonymousRecord.Baz;
  end;

  function TFoo.TestDeclaredRecord: String;
  begin
    Result := FDeclaredRecord.Baz;
  end;
  
end.
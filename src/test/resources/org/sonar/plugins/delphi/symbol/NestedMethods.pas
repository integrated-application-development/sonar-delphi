unit NestedMethods;

{This is a sample Delphi file.}

interface
implementation

function Foo(Index: Integer): Integer;
  
  function Bar(Argument: String): Integer;
  begin
    if Argument = 'String' then begin
      Result := Index;
    end;

    Result := 0;
  end;

  function Bar(Argument: Integer): String;
  begin
    if Argument = 12345 then begin
      Result := 'String';
    end;

    Result := '';
  end;

begin
  Result := Bar(Bar(12345));
end;

end.
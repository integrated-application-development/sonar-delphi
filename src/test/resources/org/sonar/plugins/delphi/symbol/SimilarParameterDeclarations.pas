unit SimilarParameterDeclarations;

{This is a sample Delphi file.}

interface

type
  TFoo = class(TObject)
  public
    procedure Bar1(Index: Integer);
    procedure Bar2(Index: Integer);
  end;

implementation

procedure TFoo.Bar1(Index: Integer);
begin
  Bar2(Index);
end;

procedure TFoo.Bar2(Index: Integer);
begin
  // Do nothing
end;

end.
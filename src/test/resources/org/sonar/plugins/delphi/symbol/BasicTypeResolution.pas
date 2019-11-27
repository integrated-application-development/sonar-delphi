unit BasicTypeResolution;

{This is a sample Delphi file.}

interface

type
  TBar = class(TObject)
  public
    constructor Create;
    procedure SomeProcedure;
  end;

  TFoo = class(TObject)
  private
    FBar: TBar;
    procedure Test;
    function GetBar: TBar;
  end;

implementation

procedure TFoo.Test;
begin
  FBar.SomeProcedure;
  GetBar.SomeProcedure;
  TBar.Create.SomeProcedure;
  GetBar.Create.SomeProcedure;
end;

function TFoo.GetBar: TBar;
begin
  Result := FBar;
end;

end.

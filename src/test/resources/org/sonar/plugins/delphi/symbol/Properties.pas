unit Properties;

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
  public
    procedure SetBar(Bar: TBar);
    function GetBar: TBar;
    property MethodBar: TBar read GetBar write SetBar;
    property FieldBar: TBar read FBar write FBar;
    property ExplicitBar: TBar read FBar write FBar;
  end;

implementation

procedure TFoo.SetBar(Bar: TBar);
begin
  FBar := Bar;
end;

function TFoo.GetBar: TBar;
begin
  Result := FBar;
end;

procedure Test(Foo: TFoo);
var
  Bar: TBar;
begin
  Foo.MethodBar := TBar.Create;
  Foo.MethodBar.SomeProcedure;

  Foo.FieldBar := TBar.Create;
  Foo.FieldBar.SomeProcedure;
  
  Foo.ExplicitBar := TBar.Create;
  Foo.ExplicitBar.SomeProcedure;
end;

end.
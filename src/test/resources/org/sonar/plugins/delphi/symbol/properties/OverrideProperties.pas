unit OverrideProperties;

{This is a sample Delphi file.}

interface

type
  TBar = class(TObject)
  public
    procedure SomeProcedure;
    property DefaultProperty[Index: Integer]: TBar; default;
  end;

  TFoo = class(TObject)
  private
    FBar: TBar;
  public
    property Bar: TBar read FBar write FBar;
  end;

  TBaz = class(TFoo)
  public
    property Bar;
    property DefaultProperty;
  end;

implementation

procedure Test(Foo: TFoo; Baz: TBaz);
begin
  Foo.Bar.SomeProcedure;
  Baz.Bar.SomeProcedure;
  Foo.Bar[0].SomeProcedure;
  Baz.Bar[0].SomeProcedure;
end;

end.
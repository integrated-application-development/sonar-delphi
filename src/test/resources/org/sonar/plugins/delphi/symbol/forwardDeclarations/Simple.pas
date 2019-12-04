unit Simple;

{This is a sample Delphi file.}

interface

  type
    TFoo = class;
    TBar = class;

    TFoo = class(TObject)
    private
      FBar: TBar;
    public
      property Bar: TBar read FBar;
    end;

    TBar = class(TObject)
      procedure Baz(Arg: Boolean);
    end;

implementation

procedure Test(Foo: TFoo; Arg: Boolean);
begin
  Foo.Bar.Baz(Arg);
end;

end.
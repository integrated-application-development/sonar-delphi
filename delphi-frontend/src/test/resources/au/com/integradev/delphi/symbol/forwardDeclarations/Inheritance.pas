unit Inheritance;

interface

  type
    TFoo = class;
    TBar = class;
    TBaseBar = class;

    TFoo = class(TObject)
    private
      FBar: TBar;
    public
      property Bar: TBar read FBar;
    end;

    TBar = class(TBaseBar)

    end;

    TBaseBar = class

    end;

implementation

procedure Proc(Obj: TObject);
begin
  // Do nothing
end;

procedure Test(Foo: TFoo; Bar: TBar);
begin
  Proc(Foo.Bar);
  Proc(Bar);
end;

end.
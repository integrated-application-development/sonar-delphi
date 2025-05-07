unit HiddenDefaultProperties;

interface

type
  TBaseBar = class(TObject)
    property DefaultProperty[AIndex: Integer]: TBaseBar; default;
  end;

  TBar = class(TBaseBar)
    procedure Baz;
    property DefaultProperty[Index: Integer]: TBar; default;
  end;

  TFoo = class(TBar)
    property DefaultProperty[Index: Integer]: TBar; default;
  end;

  TXyzzyz = class
    property SomeProperty: TFoo;
  end;

implementation

procedure Test(Xyzzyz: TXyzzyz);
begin
  Xyzzyz.SomeProperty[0].Baz;
end;

end.
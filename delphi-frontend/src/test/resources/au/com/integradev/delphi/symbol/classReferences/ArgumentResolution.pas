unit ArgumentResolution;

interface

type
  TFoo = class(TObject)
  public
    class procedure Baz;
  end;

  TBar = class(TFoo)
  end;

  TMetaFoo = class of TFoo;

implementation

procedure AcceptFooType(Meta: TMetaFoo);
begin
  // ...
end;

procedure Test;
begin
  AcceptFooType(TFoo);
  AcceptFooType(TBar);
end;


end.
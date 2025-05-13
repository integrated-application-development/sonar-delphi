unit ConstructorTypeResolution;

interface

type
  TFoo = class(TObject)
  public
    constructor Create(Bar: Boolean);
  end;

  TMetaFoo = class of TFoo;

implementation

procedure Proc(Obj: TObject);
begin
  
end;

procedure Test(Bar: Boolean; Baz: TMetaFoo);
begin
  Proc(Baz.Create(Bar));
  Proc(TMetaFoo.Create(Bar));
end;

end.
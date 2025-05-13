unit Casts;

interface

type
  TFoo = class(TObject)
  private
    procedure Bar;
  end;

  TFooClass = class of TFoo;

implementation

function GetClass: TFooClass;
begin
  Result := TFoo;
end;

procedure Test(Arg: TObject);
begin
  TFoo(Arg).Bar;
  (Arg as TFoo).Bar;
  (Arg as GetClass).Bar;
end;

end.

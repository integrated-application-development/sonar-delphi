unit Casts;

{This is a sample Delphi file.}

interface

type
  TFoo = class(TObject)
  private
    procedure Bar;
  end;

implementation

procedure Test(Arg: TObject);
begin
  TFoo(Arg).Bar;
  (Arg as TFoo).Bar;
end;

end.

unit TypeParameterNameConflict;

interface

type
  IFoo = interface
    procedure Foo;
  end;

  IBar = interface
    procedure Bar;
  end;

  TGeneric<T: IFoo> = class
    FData: T;
    procedure Func<T: IBar>(Arg: T);
  end;

implementation

procedure TGeneric<T>.Func<T>(Arg: T);
var
  BarVar: T;
begin
  FData.Foo;
  Arg.Bar;
  BarVar.Bar;
end;

end.


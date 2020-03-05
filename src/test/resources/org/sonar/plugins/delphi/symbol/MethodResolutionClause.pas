unit MethodResolutionClause;

{This is a sample Delphi file.}

interface

type
  MyInterface1 = interface(IInterface)
    procedure Func1(Arg: String); overload;
  end;

  MyInterface = interface(IInterface)
    procedure Func1(Arg: Double); overload;
  end;

  MyImplementation = class(TObject, MyInterface)
    procedure Implementation1(Arg: String); overload;
    procedure Implementation1(Arg: Double); overload;
    procedure MyInterface.Func1 = Implementation1;

    procedure Func1(Arg: Double); overload;
  end;

implementation

end.

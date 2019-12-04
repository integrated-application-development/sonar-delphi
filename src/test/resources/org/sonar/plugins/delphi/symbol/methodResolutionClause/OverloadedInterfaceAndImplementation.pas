unit OverloadedInterfaceAndImplementation;

{This is a sample Delphi file.}

interface

type
  MyInterface = interface
    procedure InterfaceProc(Arg: String); overload;
    procedure InterfaceProc(Arg: Double); overload;
  end;

  MyImplementation = class(TObject, MyInterface)
    procedure ImplementationProc(Arg: String); overload;
    procedure ImplementationProc(Arg: Double); overload;
    procedure MyInterface.InterfaceProc = ImplementationProc;

    procedure InterfaceProc(Arg: Double); overload;
  end;

implementation

end.

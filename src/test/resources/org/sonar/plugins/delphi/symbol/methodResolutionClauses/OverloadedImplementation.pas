unit OverloadedImplementation;

{This is a sample Delphi file.}

interface

type
  MyInterface = interface
    procedure InterfaceProc(Arg: String);
  end;

  MyImplementation = class(TObject, MyInterface)
    procedure ImplementationProc(Arg: String); overload;
    procedure ImplementationProc(Arg: Double); overload;
    procedure MyInterface.InterfaceProc = ImplementationProc;
  end;

implementation

end.

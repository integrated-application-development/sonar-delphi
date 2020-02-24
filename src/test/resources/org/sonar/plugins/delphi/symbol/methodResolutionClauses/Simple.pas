unit Simple;

{This is a sample Delphi file.}

interface

type
  MyInterface = interface
    procedure InterfaceProc(Arg: String);
  end;

  MyImplementation = class(TObject, MyInterface)
    procedure ImplementationProc(Arg: String);
    procedure MyInterface.InterfaceProc = ImplementationProc;
  end;

implementation

end.

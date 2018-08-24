unit InterfaceMethodResolutionClause;

interface

type
  IInterfaceA = interface
    ['{D9434374-0D61-44A6-A84B-06F574F140BE}']
    procedure DoSomething;
  end;

  IInterfaceB = interface
    ['{D9434374-0D61-44A6-A84B-06F574F140BE}']
    procedure DoSomethingToo;
  end;

  TMyClass = class(TInterfacedObject, IInterfaceA, IInterfaceB)
  public
    procedure DoSomething;
    procedure IInterfaceB.DoSomethingToo = DoSomething;
  end;

implementation

{ TMyClass }

procedure TMyClass.DoSomething;
begin
  Writeln('Do Something');
end;

end.

unit System.Classes;

interface

uses
  System.Types;

type
  TPersistent = class(TObject)

  end;

  TComponentClass = class of TComponent;

  TComponent = class(TPersistent, IInterface, IInterfaceComponentReference)
 
  end;

implementation

end.